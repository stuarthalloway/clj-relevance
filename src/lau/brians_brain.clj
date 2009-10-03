; adapted from http://blog.bestinclass.dk/index.php/2009/10/brians-functional-brain/
(ns lau.brians-brain
  (:use clojure.contrib.str-utils clojure.contrib.seq-utils)
  (:require [clojure.contrib.jmx :as jmx])
  (:import [clojure.contrib.jmx Bean]))

(import '(javax.swing JFrame JPanel)
        '(java.awt Color Graphics)
        '(java.awt.image BufferedImage))

(def dim-board   [ 90   90])
(def dim-screen  [600  600])
(def dim-scale   (vec (map / dim-screen dim-board)))

; doseq instead?
(defn fmap [f coll] (doall (map f coll)))

(defn cell-indexed [board]
  (for [[row-idx row] (indexed board)]
    (for [[col-idx val] (indexed row)]
         [val row-idx col-idx])))

(defn unindexed [board]
  (for [row board]
    (for [[col] row] col)))

(def state->char {:on \O, :dying \|, :off \.})
(def char->state
     (into {} (map (fn [[k v]] [v k]) state->char)))

(defn board->chars
  [aboard]
  (map (partial map state->char) aboard))

(defn board->str
  [aboard]
  (str-join "\n" (map (partial str-join "") (board->chars aboard))))

(defn str->board [s]
  (map (partial map char->state)
       (map #(re-gsub #"\s+" "" %) (re-split #"\n" s))))

(defn render-cell [#^Graphics g cell]
  (let [[state x y] cell
        x  (inc (* x (dim-scale 0)))
        y  (inc (* y (dim-scale 1)))]
    (doto g
      (.setColor (if (= state :dying) Color/GRAY Color/WHITE))
      (.fillRect x y (dec (dim-scale 0)) (dec (dim-scale 1))))))

(defn render [g img bg stage]
  (.setColor bg Color/BLACK)
  (.fillRect bg 0 0 (dim-screen 0) (dim-screen 1))
  (fmap (fn [col]
          (fmap #(when (not= :off (% 0))
                   (render-cell bg %)) col)) (cell-indexed stage))
  (.drawImage g img 0 0 nil))

(defn new-board
  ([] (apply new-board dim-board))
  ([dim-x dim-y]
     (for [x (range dim-x)]
       (for [y (range dim-y)]
         (if (< 50 (rand-int 100)) :on :off)))))

(defn active-neighbors [above [left _ right] below]
  (count
   (filter #(= :on %)
           (concat above [left right] below))))

(defn torus-window [coll]
  (partition 3 1 (concat [(last coll)] coll [(first coll)])))

(defn rules [above [_ cell _ :as row] below]
  (cond
   (= :on    cell)                              :dying
   (= :dying cell)                              :off  
   (= 2 (active-neighbors above row below))     :on   
   :else                                        :off  ))

(defn step [board]
  (doall
   (pmap (fn [window]
          (apply #(doall (apply map rules %&))
                 (doall (map torus-window window))))
        (torus-window board))))

(def sim-status (ref {:latest ""}))
(jmx/register-mbean (Bean. sim-status) "lau.brians-brain:name=Sim")

(defn update-stage [stage]
  (swap! stage step)
  (dosync (alter sim-status assoc :latest (board->str @stage))))

(defn activity-loop [surface stage]
  (while
   true
   (update-stage stage)
   (.repaint surface)))

(defn launch [] 
  (let [stage (atom (new-board))
        frame (JFrame.)
        img   (BufferedImage. (dim-screen 0) (dim-screen 1) (BufferedImage/TYPE_INT_ARGB))
        bg    (.getGraphics img)
        panel (doto (proxy [JPanel] [] (paint [g] (render g img bg @stage))))]
    (doto frame (.add panel) .pack (.setSize (dim-screen 0) (dim-screen 1)) .show
          (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE))
    (future (activity-loop panel stage))
    stage))

(defn launch-console []
  (let [stage (atom (new-board))]
    (while true
           (swap! stage step)
           (Thread/sleep 100)
           (println (board->str @stage)))))




