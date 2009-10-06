(in-ns 'lau.brians-brain)

(defn launch-console []
  (doseq [board (iterate step (new-board))]
    (println (board->str board))))
