(in-ns 'lau.brians-brain)

(def dim-board   [ 90   90])
(def dim-screen  [600  600])
(def dim-scale   (vec (map / dim-screen dim-board)))

(defn with-coords [board]
  (for [[row-idx row] (indexed board)]
    (for [[col-idx val] (indexed row)]
         [val row-idx col-idx])))

(defn without-coords [board]
  (for [row board]
    (for [[state] row] state)))

(def state->char {:on \O, :dying \|, :off \.})
(def char->state
     (into {} (map (fn [[k v]] [v k]) state->char)))

(defn board->chars
  [board]
  (map (partial map state->char) board))

(defn board->str
  "Convert from board form to string form:

   O.O         [[ :on     :off  :on    ]
   |.|    <==   [ :dying  :off  :dying ]
   O.O          [ :on     :off  :on    ]
"
  [board]
  (str-join "\n" (map (partial str-join "") (board->chars board))))

(defn str->board
  "Convert from string form to board form:

   O.O         [[ :on     :off  :on    ]
   |.|    ==>   [ :dying  :off  :dying ]
   O.O          [ :on     :off  :on    ]
"
  [s]
  (map (partial map char->state)
       (map #(re-gsub #"\s+" "" %) (re-split #"\n" s))))

(defn new-board
  "Create a new board with about half the cells set to :on."
  ([] (apply new-board dim-board))
  ([dim-x dim-y]
     (for [x (range dim-x)]
       (for [y (range dim-y)]
         (if (< 50 (rand-int 100)) :on :off)))))

