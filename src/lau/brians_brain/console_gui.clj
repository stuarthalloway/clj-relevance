(in-ns 'lau.brians-brain)

(defn launch-console []
  (let [stage (atom (new-board))]
    (while true
           (swap! stage step)
           (Thread/sleep 100)
           (println (board->str @stage)))))
