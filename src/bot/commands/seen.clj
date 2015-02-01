;(def ^:private seen-file (:seen-db env/env))
;
;(defn- set-seen [nick]
;  (let [file (iota/vec seen-file)
;        index (.indexOf file (first (filter #(.startsWith % nick) file)))]
;    (if (neg? index)
;      (spit seen-file (str (format "%-32s" nick) (format "%-20s" (local/local-now)) "\n") :append true)
;      )))
;
;(defn- get-seen [nick]
;  (filter #(.startsWith % nick) (clojure.string/split-lines (slurp seen-file))))
;
;(defn seen
;  "Find out when a user was last seen on IRC."
;  [query message]
;  (when (not (clojure.string/blank? query))
;
;    )
;  )
