(ns bot.bot
  (:require [clj-http.client :as http]
            [irclj.core :as ircc]
            [clojure.tools.logging :as log]
            [environ.core :as env])
  (:use [bot.commands.core :only (auto-complete-command)]
        [bot.core]))

(def url-regexes
  [#"https?://(?:www\.)?youtube\.com/\S*"
   #"https?://youtu\.be/\S*"
   #"https?://open\.spotify\.com/\S*"
   #"https?://(?:www\.)?vimeo.com/\S*"
   #"https?://news\.ycombinator.com/\S*"
   #"https?://(?:www\.)?aftonbladet.se/\S*"
   #"https?://(?:www\.)?imdb.com/title/\S*"])

(defn privmsg [irc message]
  (if (.startsWith (:text message) ".")
    (let [tokens (clojure.string/split (:text message) #"\s")
          command-name (subs (first tokens) 1)
          params (clojure.string/join " " (drop 1 tokens))
          found-commands (auto-complete-command command-name)
          result (cond
                  (.startsWith (:text message) ". ") (try (bot.commands/unified-search params message) (catch Exception e (log/error e)))
                  (== (count found-commands) 1) (try ((first found-commands) params message) (catch Exception e (log/error e)))
                  (> (count found-commands) 1) "Multiple matches")]
      (cond
       (is-valid-seq-result result) (dorun (map #(ircc/reply irc message (bot.core/truncate-privmsg %)) result))
       (is-valid-string-result result) (ircc/reply irc message (bot.core/truncate-privmsg result)))
      )
    (try
      (when-let [titles (url-titles (:text message) url-regexes)]
        (dorun (map #(ircc/reply irc message (str "Title: " %)) titles)))
      (catch Exception e (log/error e)))))

;(defn on-exception [irc e]
;  (System/exit -1))

(defn start-bot [connections]
  (log/info "Connecting to servers...")
  (dorun (map #(let [irc (ircc/connect (:host %) (:port %) (:nick %)
                                       :ssl? (:ssl? %)
                                       :callbacks {:privmsg privmsg})]
                                       ;:callbacks {:privmsg privmsg :on-exception on-exception})]
                 (log/info "Connected to" (:host %))
                 (doseq [channel (:channels %)] (ircc/join irc channel))
                 (log/info "Channels joined:" (:channels %)))
              connections)))
