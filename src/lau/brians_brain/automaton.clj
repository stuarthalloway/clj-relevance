(in-ns 'lau.brians-brain)

(defn active-neighbors
  "Count the active (:on) neighbors one cell away from me in
   any direction. Maximum of 8."
  [above [left _ right] below]
  (count
   (filter #(= :on %)
           (concat above [left right] below))))

(defn torus-window
  "The torus window is a cursor over the board, with each item
   containining a cell and all its immediate neighbors."
  [coll]
  (partition 3 1 (concat [(last coll)] coll [(first coll)])))

(defn rules
  "Determine the cells next state based on its current
   state and number of active neighbors."
  [above [_ cell _ :as row] below]
  (cond
   (= :on    cell)                              :dying
   (= :dying cell)                              :off  
   (= 2 (active-neighbors above row below))     :on   
   :else                                        :off  ))

(defn step
  "Advance the automation by one step, updating all cells."
  [board]
  (doall
   (pmap (fn [window]
          (apply #(doall (apply map rules %&))
                 (doall (map torus-window window))))
        (torus-window board))))

(def status (ref {:latest ""}))
(defn register-status-mbean
  "Gratuitous demo of JMX. Throw the most recent automaton status, as a string,
   into a JMX bean so other processes can query it."
  []
  (jmx/register-mbean (Bean. status) "lau.brians-brain:name=Sim"))

(defn update-stage
  "Update a reference."
  [stage]
  (swap! stage step)
  (dosync (alter status assoc :latest (board->str @stage))))
