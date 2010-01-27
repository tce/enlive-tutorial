(ns tutorial.template3
  (:require [net.cgrand.enlive-html :as html])
  (:use [clojure.contrib.java-utils :only [file]])
  (:use compojure))

;; =============================================================================
;; Top Level Defs
;; =============================================================================

;; change this line to reflect your setup
(def *webdir* "/Users/davidnolen/development/clojure/enlive-tutorial/src/tutorial/")

(def *hits* (atom 0))

(defmacro block [sym]
  `(fn [n#] (if (nil? ~sym) n# ((html/substitute ~sym) n#))))

(defmacro maybe-content [sym]
  `(fn [n#] (if (nil? ~sym) n# ((html/content ~sym) n#))))

;; =============================================================================
;; The Templates Ma!
;; =============================================================================

(html/deftemplate base (file *webdir* "base.html")
  [{title :title, header :header, body :body, footer :footer :as ctxt}]
  [:#title]      (maybe-content title)
  [:#header]     (block header)
  [:#body]       (block body)
  [:#footer]     (block footer))

(html/defsnippet link-model (file *webdir* "3col.html")  [:ol#links :> html/first-child]
  [[text href]] 
  [:a] (html/do->
        (html/content text) 
        (html/set-attr :href href)))

(html/defsnippet body (file *webdir* "3col.html") [:div#body]
  [{left :left, middle :middle, right :right :as context}]
  [:div#left]   (block left)
  [:div#middle] (block middle)
  [:div#right]  (block right))

;; =============================================================================
;; Pages
;; =============================================================================

(defn pagea [{title :title :as ctxt}]
     (base {:title title
            :body  (body ctxt)}))

(def pageb-context
     {:time "Funner Time"
      :links [["Clojure" "http://www.clojure.org"]
              ["Compojure" "http://www.compojure.org"]
              ["Clojars" "http://www.clojars.org"]
              ["Enlive" "http://github.com/cgrand/enlive"]]})

(defn pageb [ctxt]
     (base {:body (body ctxt)}))

(defn index
  ([] (base {}))
  ([ctxt] (base ctxt)))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes example-routes
  (GET "/"
    (apply str (index)))
  (GET "/a/"
    (apply str (pagea {:title "Page A"})))
  (GET "/b/"
    (apply str (pagea pageb-context)))
  (ANY "*"
    [404 "Page Not Found"]))

;; =============================================================================
;; The App
;; =============================================================================

(def *app* (atom nil))

(defn start-app []
  (if (not (nil? @*app*))
    (stop @*app*))
  (reset! *app* (run-server {:port 8080}
                            "/*" (servlet example-routes))))

(defn stop-app []
  (stop @*app*))