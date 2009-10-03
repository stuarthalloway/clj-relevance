; adapted from http://blog.bestinclass.dk/index.php/2009/10/brians-functional-brain/
(ns lau.brians-brain
  (:use clojure.contrib.str-utils clojure.contrib.seq-utils)
  (:require [clojure.contrib.jmx :as jmx])
  (:import [clojure.contrib.jmx Bean]))

(import '(javax.swing JFrame JPanel)
        '(java.awt Color Graphics)
        '(java.awt.image BufferedImage))

(load "brians_brain/board")
(load "brians_brain/automaton")
(load "brians_brain/swing_gui")
(load "brians_brain/console_gui")






