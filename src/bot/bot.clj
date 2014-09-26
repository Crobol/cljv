(ns bot.bot
  (:require [clj-http.client :as http]
            [irclj.core :as ircc]
            [clojure.tools.logging :as log]
            [environ.core :as env])
  (:use [bot.commands.core :only (auto-complete-command)]
        [bot.core]))

(def url-regexes
  [#"https?://(?:www\.)?youtube\.com/\S*"
   #"https?://open\.spotify\.com/\S*"
   #"https?://(?:www\.)?vimeo.com/\S*"
   #"https?://news\.ycombinator.com/\S*"
   #"https?://(?:www\.)?aftonbladet.se/\S*"
   #"https?://(?:www\.)?imdb.com/title/\S*"])

(defn privmsg [irc message]
  (log/info (str (:nick message) "> " (:text message)))
  (if (.startsWith (:text message) ".")
    (let [tokens (clojure.string/split (:text message) #"\s")
          command-name (subs (first tokens) 1)
          params (clojure.string/join " " (drop 1 tokens))
          found-commands (auto-complete-command command-name)
          result (cond
                  (== (count found-commands) 1) (try ((first found-commands) params message) (catch Exception e (log/error e)))
                  (> (count found-commands) 1) "Multiple matches")]
      (cond
       (is-valid-coll-result result) (dorun (map #(ircc/reply irc message (str %)) result))
       (is-valid-string-result result) (ircc/reply irc message result))
      )
    (try
      (when-let [titles (url-titles (:text message) url-regexes)]
        (dorun (map #(ircc/reply irc message (str "Title: " %)) titles)))
      (catch Exception e (log/error e)))))

(defn start-bot [connections]
  (log/info "Connecting to servers...")
  (dorun (map #(let [irc (ircc/connect (:host %) (:port %) (:nick %)
                                       :ssl? (:ssl? %)
                                       :callbacks {:privmsg privmsg})]
                 (log/info "Connected to" (:host %))
                 (ircc/join irc (first (:channels %)))
                 (log/info "Channels joined:" (:channels %)))
              connections)))
