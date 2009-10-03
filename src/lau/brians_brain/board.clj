(in-ns 'lau.brians-brain)

(def dim-board   [ 90   90])
(def dim-screen  [600  600])
(def dim-scale   (vec (map / dim-screen dim-board)))

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
  "Convert from board form to string form:

   O.O         [[ :on     :off  :on    ]
   |.|    <==   [ :dying  :off  :dying ]
   O.O          [ :on     :off  :on    ]
"
  [aboard]
  (str-join "\n" (map (partial str-join "") (board->chars aboard))))

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

