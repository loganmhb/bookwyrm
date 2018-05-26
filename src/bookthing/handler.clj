(ns bookthing.handler
  (:require [bookthing.db :as db]
            [buddy.hashers :as hashers]
            [clojure.java.jdbc :as jdbc]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [mount.core :as mount]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :as response])
  (:import org.postgresql.util.PSQLException))

(defn render-page [contents]
  (-> (hiccup/html [:html
                    [:head
                     [:title "Book Thing"]
                     [:link {:rel "stylesheet" :href "/css/main.css"}]]
                    [:body
                     [:div {:class "container"}
                      contents]]])
      response/response
      (response/content-type "text/html")))

(defn home [request]
  (render-page
   [:div
    [:h1 "Bookshelf Thing"]
    [:p "A website for books"]
    (if-let [username (get-in request [:session :username])]
      [:div
       [:p (format "Hello, %s!" username)]
       [:form {:action "/logout" :method "POST"}
        (anti-forgery-field)
        [:button "Log out"]]]
      [:div
       [:div [:a {:href "/signup"} "Sign up"]]
       [:div] [:a {:href "/login"} "Log in"]])]))

(defn signup-page []
  (render-page
   [:div
    [:h1 "Sign up"]
    [:form {:method "POST"}
     (anti-forgery-field)
     [:label {:for "username"} "Username"]
     [:input {:id "username" :type "text" :name "username"}]
     [:label {:for "password"} "Password"]
     [:input {:id "password" :type "password" :name "password"}]
     [:div
      [:button {:type "submit"} "Sign up"]]]]))

(defn signup-user [request]
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])]
    (try
      (jdbc/insert! db/db-spec :users {:username username :password_hash (hashers/derive password)})
      {:status 200 :body "ok"}
      (catch PSQLException e
        (if (.contains (.getMessage e) "already exists")
          (render-page [:p "That username is taken."])
          {:status 500})))))

(defn login-page []
  (render-page
   [:div
    [:h1 "Log in"]
    [:form {:method "POST"}
     (anti-forgery-field)
     [:label {:for "username"} "Username"]
     [:input {:id "username" :type "text" :name "username"}]
     [:label {:for "password"} "Password"]
     [:input {:id "password" :type "password" :name "password"}]
     [:div
      [:button {:type "submit"} "Log in"]]]]))

(defn login-user [request]
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])
        password-hash (->> ["SELECT password_hash FROM users WHERE username = ?" username]
                           (jdbc/query db/db-spec)
                           first
                           :password_hash)]
    (cond
      (nil? password-hash)
      (render-page "Username taken" "That username does not exist.")

      (hashers/check password password-hash)
      (-> (response/redirect "/")
          (assoc-in [:session :username] username))

      :else
      (render-page "Invalid password" "Invalid password"))))

(defn logout-user [request]
  (-> (response/redirect "/")
      (assoc :session nil)))

(defroutes app-routes
  (GET "/" request (home request))
  (GET "/signup" [] (signup-page))
  (POST "/signup" request (signup-user request))
  (GET "/login" [] (login-page))
  (POST "/login" request (login-user request))
  (POST "/logout" request (logout-user request))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(mount/defstate server
  :start (jetty/run-jetty #'app {:port 3000 :join? false})
  :stop (.stop server))

#_(mount/start)
#_(mount/stop)
