Last week, Lau wrote two excellent sample apps (and [blog](http://blog.bestinclass.dk/index.php/2009/10/brians-functional-brain/) [posts](http://blog.bestinclass.dk/index.php/2009/10/brians-transient-brain/)) demonstrating Brian's Brain in Clojure. Continuing with the first version of that example, I am going to demonstrate

* using different data structures
* visual unit tests
* JMX integration (gratuitous!)
* an approach to Clojure source code organization

Make sure you read through Lau's [original post](http://blog.bestinclass.dk/index.php/2009/10/brians-functional-brain/) first, and understand the code there.

## Data Structures

In a functional language like Clojure, it is easy to experiment with using different structures to represent the same data. Rather than being hidden in a rat's nest of mutable object relationships, your data is right in front of you in simple persistent data structures. In Lau's implementation, the board is represented by a list of lists, like so:

<pre><code class="clojure">(([:on 0 0] [:off 0 1] [:on 0 2]) 
 ([:on 1 0] [:on 1 1] [:off 1 2]))
</code></pre>

Each cell in the list knows its state (`:on`, `:off`, or `:dying`), and its x and y coordinates on the board. The board data structure is used for two purposes:

* the `step` function applies the rules of the automaton, returning the board's next state
* the `render` function draws the board on a Swing panel

These two functions have slightly different needs: the `step` function cares only the state of adjacent cells, and can ignore the coordinates, while the `render` function needs both.

How hard would it be to convert the data to a form just stores only the state? Not hard at all:

<pre><code class="clojure">(defn without-coords [board]
  (for [row board]
    (for [[state] row] state)))
</code></pre>

You could write `without-coords` as a one-liner using `map`, but I prefer how the nested `for`s visually call out the fact that you are manipulating two dimentional data.

Without the coords, the board is easier to read:

<pre><code class="clojure">((:on :off :on)) 
 (:on :on :off))
</code></pre>

If you choose to store the board this way, you will need to get the coordinates back for rendering. That's easy too, again using nested `for`s to demonstrate that you are transforming two-dimensional data:

<pre><code class="clojure">(defn with-coords [board]
  (for [[row-idx row] (indexed board)]
    (for [[col-idx val] (indexed row)]
         [val row-idx col-idx])))
</code></pre>

So, with a couple of tiny functions, you can easily convert between two different representations of the data. Why not use both formats, picking the right one for each function's needs?

When performance is critical, there is another advantage to using different data formats: caching. Consider the `step` funtion, which uses the `rules` function to determine the next value of each cell:

<pre><code class="clojure">(defn rules
  [above [_ cell _ :as row] below]
  (cond
   (= :on    cell)                              :dying
   (= :dying cell)                              :off  
   (= 2 (active-neighbors above row below))     :on   
   :else                                        :off  ))
</code></pre>

The "without coordinates" format used by `step` and `rules` passes only exactly the data needed. As a result, the universe of legal inputs to `rules` is small enough to fit in a small cache in memory. And in-memory caching is trivial in Clojure, simply call `memoize` on a function. (It turns out that for this particular example, the calculation is simple enough that `memoize` won't buy you anything. Lau's [second post](http://blog.bestinclass.dk/index.php/2009/10/brians-transient-brain/) demonstrates more useful optimizations: transients and double-buffering. But in some problems a cacheable funtion result is a performance lifesaver.)

If you use comprehensions such as Clojure's `for` to convert inputs to exactly the data a function needs, your functions will be simpler to read and write. This "caller makes right" approach is not always appropriate. When it is appropriate, it is far less tedious to implement than the related adapter pattern from OO programming.

Since multiple data formats are so easy, you can use yet another format for testing.

## Testing

Brian's Brain is a simulation in two dimensions, it would be nice to write tests with a literal, visual, 2-d representation. In other words:

<pre><code class="clojure">; this sucks
(is (= :on (rules (cell-with-two-active-neighbors))))

; this rocks
O..
...  => O     
..O
</code></pre>

In the literal form above the `O` is an `:on` cell, and the `.` is an `:off` cell.

Creating this representation is easy. The `board->str` function converts a board to a compact string form:

<pre><code class="clojure">(defn board->str
  "Convert from board form to string form:

   O.O         [[ :on     :off  :on    ]
   |.|     ==   [ :dying  :off  :dying ]
   O.O          [ :on     :off  :on    ]]
"
  [board]
  (str-join "\n" (map (partial str-join "") (board->chars board))))
</code></pre>

The `board->chars` helper is equally simple:

<pre><code class="clojure">(def state->char {:on \O, :dying \|, :off \.})
(defn board->chars
  [board]
  (map (partial map state->char) board))
</code></pre>

With the new stringified board format, you can trivially write tests like this:

<pre><code class="clojure">(deftest test-rules
  (are [result boardstr] (= result (apply rules (str->board boardstr)))
       :dying  "...
                .O.
                ..."

       :off    "O.O
                ...
                O.O"

       :on     "|||
                O.O
                |||"))
</code></pre>

The `are` macro makes is simply to run the same tests over multiple inputs, and with liberal use of whitespace the tests line up visually. It isn't perfect, but I think it is good enough.

One last note: the string format used in tests is basically ASCII art, so you can have a console based GUI almost for free:

<pre><code class="clojure">(defn launch-console []
  (doseq [board (iterate step (new-board))]
    (println (board->str board))))
</code></pre>

## JMX Integration

Ok, JMX integration is gratuitous for an example like this. But clojure.contrib.jmx is so easy to use I couldn't resist. You can store the total number of iterations perfomed in a thread-safe Clojure atom:

<pre><code class="clojure">(def status (atom {:iterations 0}))
</code></pre>

Then, just expose the atom as a JMX mbean. 

<pre><code class="clojure">(defn register-status-mbean []
  (jmx/register-mbean (Bean. status) "lau.brians-brain:name=Automaton"))
</code></pre>

Yes, it is that easy. Create any Clojure reference type, point it at a map, and register a bean. You can now access the iteration counter from a JMX client such as the jconsole application that ships with the JDK.

To make the mbean report real data, wrap the automaton's iterations in an `update-stage` helper function that both does the work, and updates the counter.

<pre><code class="clojure">(defn update-stage
  "Update the automaton (and associated metrics)."
  [stage]
  (swap! stage step)
  (swap! status update-in [:iterations] inc))
</code></pre>

If you haven't seen `update-in` (and its cousins `get-in` and `assoc-in`) before, go and study them for a moment now. They make working with non-trivial data structures a joy.

You might disagree with my choice of atoms. With a pair of references, you could keep the iteration count exactly coordinated with the simulator. Or, with a reference plus an agent you push the work of updating the iteration count out of the main loop. Whatever you choose, Clojure makes it easy to both (a) implement state and (b) keep the statefulness separate from the bulk of your code. 

## Source Code Organization

Lau's original code weighed in at a trim 67 lines. Now that the app supports three different data formats, a console UI, and JMX integration, it is up to around 150 lines. How should we organize such a monster of an app?  Two obvious choices are: 

* put everything in one file and one namespace
* split out namespaces by functional area, e.g. automaton, swing gui, and console gui

I don't love either approach. The single file approach is confusing for the reader, because there are multiple different things going on. The multiple namespace approach is a pain for callers, because they get weighed down under a bunch of namespaces to do a single thing. 

A third option is `immigrate`. With `immigrate` you can organize your code into multiple namespaces for the benefit of readers, and then immigrate them all into a blanket namespace for casual users of the API. But immigrate may be [too cute](http://groups.google.com/group/compojure/browse_thread/thread/400aac94e536e633#) for their own good.

Instead, I chose to use one namespace for the convenience of callers, and mutilple files to provide sub-namespace organization for readers of code. I mimiced the structure [Tom Faulhaber](http://github.com/tomfaulhaber) used in clojure-contrib's pprint library: a top level file that calls `load` on several files in a subdirectory of the same name (minus the .clj extension):

<pre><code>lau/brians_brain.clj  
lau/brians_brain/automaton.clj
lau/brians_brain/board.clj
lau/brians_brain/console_gui.clj
lau/brians_brain/swing_gui.clj
</code></pre>

I also used this layout for clojure-contrib's JMX library.

## Parting Shots

Over the course of Lau's two exampples and this one, you have seen:

* an initial working application in under 100 lines of code
* transforming data structures for performance optimization
* transforming data structures for readability
* a second (console) gui
* optimizing the Swing gui with double buffering
* optimizing with transients
* visual tests
* easy addition of monitoring with JMX

And here are some things you haven't seen:

* classes
* interfaces
* uncontrolled mutation
* broken concurrency

Would it be possible to write a threadsafe Brian's Brain using mutable OO? Of course. Is there a benefit to doing so? I would love to hear your thoughts on the subject, especially in the form of code.

## Further Reading

* [Brians functional brain](http://blog.bestinclass.dk/index.php/2009/10/brians-functional-brain/) (Lau's original post)
* [Brians Transient! Brain](http://blog.bestinclass.dk/index.php/2009/10/brians-transient-brain/) (Lau's followup)
* The [clj-relevance](http://github.com/stuarthalloway/clj-relevance) repos has the completed code from this blog post at `src/lau/brians_brain`, and will be the home for future Clojure examples on this blog
* [Programming Clojure](http://www.pragprog.com/titles/shcloj/programming-clojure)



