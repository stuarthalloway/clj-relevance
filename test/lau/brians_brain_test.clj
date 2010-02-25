(ns lau.brians-brain-test
  (use circumspec lau.brians-brain clojure.contrib.str-utils))

(testing board->str
  (for-these [board str] (= (re-gsub #" " "" str) (board->str board))
       [[:on]]
       "O"

       [[:on :on]
        [:off :off]]
       "OO
        .."

       ))

(testing str->board
  (for-these [board str] (= board (str->board str))
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

(testing with-coords
  (for-these [result input] (= result (with-coords input))
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

(testing same-structure?
  (for-these [result s1 s2] (= result (same-structure? s1 s2))
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

(testing new-board
  (for-these [structure x y] (same-structure? structure (new-board x y))
       []
       0 0

       [[0]]
       1 1

       [[1 2 3] [1 2 3]]
       2 3))

(testing without-coords
  (for-these [result input] (= result (without-coords input))
       [[:a :b] [:c :d]]
       [[[:a 0 0] [:b 0 1]] [[:c 1 0] [:d 1 1]]]))

(testing active-neighbors
  (for-these [result boardstr] (= result (apply active-neighbors (str->board boardstr)))
       0 "...
          ...
          ..."

       4 "O.O
          ...
          O.O"

       2 "|||
          OOO
          |||"))

(testing rules
  (for-these [result boardstr] (= result (apply rules (str->board boardstr)))
       :dying  "...
                .O.
                ..."

       :off    "O.O
                ...
                O.O"

       :on     "|||
                O.O
                |||"))
