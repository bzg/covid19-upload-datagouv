(ns covid19.upload-datagouv
  (:require  [babashka.curl :as curl]
             [java-time :as t]
             [clojure.string :as s])
  (:gen-class))

(def datagouv-api "https://www.data.gouv.fr/api/1")

(def datagouv-api-token (System/getenv "DATAGOUV_API_TOKEN"))

;; https://www.data.gouv.fr/fr/admin/dataset/5e723be64ea6945002c092dc
(def datagouv-covid19-dataset "5e723be64ea6945002c092dc")

(def datagouv-endpoint-format
  (str datagouv-api
       "/datasets/"datagouv-covid19-dataset
       "/resources/%s/upload/"))

(def datagouv-api-headers
  {:headers {"Accept"    "application/json"
             "X-Api-Key" datagouv-api-token}})

(def spf-files-urls
  #{"file1.csv"})

(def datagouv-covid19-resources
  {"file1.csv"
   {:resource "26db01a9-891c-4b41-be85-d597b949a6c0" }})

(defn get-filename-from-url [url]
  (s/replace url #"^.+/([^/]+)$" "$1"))

(defn download-spf-files [files]
  (println "Downloading SPF files...")
  (doseq [f files]
    (let [f-n (get-filename-from-url f)]
      (try (curl/get f {:raw-args ["--output" f-n] })
           (catch Exception _
             (println "Error while downloading" f-n)))))
  (println "Downloading SPF files... done"))

(defn upload-spf-files [files]
  (println "Uploading SPF files...")
  (doseq [f files]
    (let [f-n      (get-filename-from-url f)
          resource (get-in datagouv-covid19-resources
                           [f-n :resource])]
      (try (curl/post
            (format datagouv-endpoint-format resource)
            (merge datagouv-api-headers
                   {:form-params {"file" (str "@" f-n)}}))
           (catch Exception _
             (println "Error while uploading" f-n)))))
  (println "Uploading SPF files... done"))

(defn -main []
  (download-spf-files spf-files-urls)
  (upload-spf-files spf-files-urls))
