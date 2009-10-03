(ns lau.brians-brain-test
  (use clojure.test lau.brians-brain clojure.contrib.str-utils))

(deftest test-board->str
  (are [board str] (= (re-gsub #" " "" str) (board->str board))
       [[:on]]
       "O"

       [[:on :on]
        [:off :off]]
       "OO
        .."

       ))

(deftest test-str->board
  (are [board str] (= board (str->board str))
       [[:on]]
       "O"
       
       [[:off]]
       "."

       [[:dying]]
       "|"
       
       [[:on :on]
        [:off :off]]
       "OO
        .."

       ))

(deftest test-cell-indexed
  (are [result input] (= result (cell-indexed input))
       [[[:a 0 0] [:b 0 1]] [[:c 1 0] [:d 1 1]]]
       [[:a :b] [:c :d]] ))

(defn structure-type [x]
  (cond
   (sequential? x)  :sequential
   (associative? x) :associative
   :default         :other))

(defmulti same-structure? #(map structure-type %&))
(defmethod same-structure? [:associative :associative] [a b]
  (and (= (keys a) (keys b))
       (every? true? (map same-structure? (vals a) (vals b)))))
(defmethod same-structure? [:sequential :sequential] [a b]
  (and (= (count a) (count b))
       (every? true? (map same-structure? a b))))
(defmethod same-structure? [:other :other] [_ _] true)
(defmethod same-structure? :default [_ _] false)

(deftest test-same-structure
  (are [result s1 s2] (= result (same-structure? s1 s2))
       true 0 1
       true [] []
       true {} {}
       true {:a 1} {:a 2}
       true [:a :b] [1 2]
       false 0 []
       false 1 {}
       false [] {}
       false [] [1]
       false {:a 1} {:b 1}))

(deftest test-new-board
  (are [structure x y] (same-structure? structure (new-board x y))
       []
       0 0

       [[0]]
       1 1

       [[1 2 3] [1 2 3]]
       2 3))

(deftest test-cell-unindexed
  (are [result input] (= result (unindexed input))
       [[:a :b] [:c :d]]
       [[[:a 0 0] [:b 0 1]] [[:c 1 0] [:d 1 1]]]))

(deftest test-active-neighbors
  (are [result boardstr] (= result (apply active-neighbors (str->board boardstr)))
       0 "...
          ...
          ..."

       4 "O.O
          ...
          O.O"

       2 "III
          OOO
          III"))