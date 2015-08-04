
(def ^:private db-format { :record-length 566 :state-offset 0 :nick-offset 1 :date-offset 33 :quote-offset 53 })
(def ^:private states {:active "1" :inactive "0"})

(def ^:private quote-options [["-a" "--add"] ["-d" "--delete INDEX"] ["-l" "--latest"]])

(def ^:private db-file (:quote-db env/env))

(defn- set-state [index state]
  (with-open [mapped-file (clj-mmap/get-mmap db-file :read-write)]
    (let [root-position (* index (:record-length db-format))
          mapped-state (get states state)]
      (clj-mmap/put-bytes mapped-file (.getBytes mapped-state (java.nio.charset.Charset/forName "UTF-8")) (+ root-position (:state-offset db-format)) 1))
    ))

(defn- save-quote [nick quote]
  (spit db-file (str "1" (format "%-32s" nick) (format "%-20s" "date-here") (format "%-512s" quote) "\n") :append true))

(defn- get-quote [index]
  (with-open [mapped-file (clj-mmap/get-mmap db-file)]
    (let [root-position (* index (:record-length db-format))
          state (String. (clj-mmap/get-bytes mapped-file root-position 1) "UTF-8")
          nick (clojure.string/trim (String. (clj-mmap/get-bytes mapped-file (+ root-position 1) 32) "UTF-8"))
          date (clojure.string/trim (String. (clj-mmap/get-bytes mapped-file (+ root-position 33) 20) "UTF-8"))
          quote (clojure.string/trim (String. (clj-mmap/get-bytes mapped-file (+ root-position 53) 512) "UTF-8"))]
      (if (= state "1")
        {:state state
         :nick nick
         :date date
         :quote quote
         }
        nil;(throw (Exception. "No quote with that index"))
        )
      )))

(defn- quote-count []
  (/ (.length (clojure.java.io/file db-file)) (:record-length db-format)))

(defn- latest-quote []
  (get-quote (dec (quote-count))))

(defn- random-quote []
  (loop [quote (get-quote (rand-int (quote-count)))
         limit-counter 5]
    (if (or (not (nil? quote)) (zero? limit-counter))
      quote
      (recur (get-quote (rand-int (quote-count))) (dec limit-counter)))
    )
  )

(defn- delete-quote [index]
  (set-state index :inactive))

(defn quote
  "Save and view quotes"
  {:options quote-options}
  [query message]
  (let [opts (cli/parse-opts (clojure.string/split query #"\s") quote-options)]
    (cond
     (flag? opts :add) (save-quote (:nick message) (clojure.string/join " " (:arguments opts)))
     (flag? opts :latest) (:quote (latest-quote))
     (option? opts :delete) (delete-quote (Integer/parseInt (get-in opts [:options :delete])))
     (not (clojure.string/blank? (first (:arguments opts)))) (:quote (get-quote (Integer/parseInt (first (:arguments opts)))))
     :else (:quote (random-quote))
     )))
