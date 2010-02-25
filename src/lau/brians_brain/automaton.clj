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


; conway
#_(defn rules
  [above [_ cell _ :as row] below]
  (let [active (active-neighbors above row below)]
    (if (= :on cell)
      (if (<= 2 active 3) :on :off)
      (if (= active 3) :on :off))))

(defn rules
  "Determine the cell's next state based on its current
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

(def status (atom {:iterations 0}))
(defn register-status-mbean
  "Gratuitous demo of JMX. Throw the most recent automaton status, as a string,
   into a JMX bean so other processes can query it."
  []
  (jmx/register-mbean (Bean. status) "lau.brians-brain:name=Automaton"))

(defn update-stage
  "Update the automaton (and associated metrics)."
  [stage]
  (swap! stage step)
  (swap! status update-in [:iterations] inc))
