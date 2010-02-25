(in-ns 'lau.brians-brain)

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
  (doseq [row (with-coords stage)
          cell row]
    (when (not= :off (cell 0))
      (render-cell bg cell)))
  (.drawImage g img 0 0 nil))

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
        panel (proxy [JPanel] [] (paint [g] (render g img bg @stage)))]
    (doto frame (.add panel) .pack (.setSize (dim-screen 0) (dim-screen 1)) .show
          (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE))
    (future (activity-loop panel stage))
    stage))

