(ns puppetlabs.services.versioned-code-service.versioned-code-service
  (:require [puppetlabs.trapperkeeper.core :as trapperkeeper]
            [puppetlabs.services.protocols.versioned-code :as vc]
            [puppetlabs.puppetserver.shell-utils :as shell-utils]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(trapperkeeper/defservice versioned-code-service
                          vc/VersionedCodeService
                          [[:ConfigService get-in-config]]

  (current-code-id
   [this environment]
   (let [code-id-script (get-in-config [:versioned-code :code-id-command])
         {:keys [exit-code stderr stdout]} (shell-utils/execute-command code-id-script [environment])]
     ; TODO Decide what to do about normalizing/sanitizing output with respect to
     ; control characters and encodings

     ;; There are three cases we care about here:
     ;; - exit code is 0 and no stderr generated: groovy. return stdout
     ;; - exit code is 0 and stderr was generated: that's fine. log an error
     ;;   about the stderr and return stdout.
     ;; - exit code is non-zero: oh no! log an error with all the details and
     ;;   return nil
     (if (zero? exit-code)
       (do
         (when-not (string/blank? stderr)
           (log/errorf "Error output generated while calculating code id. command executed: '%s', stderr: '%s'" code-id-script stderr))
         (string/trim-newline stdout))
       (do
         (log/errorf "Non-zero exit code returned while calculating code id. command executed: '%s', exit-code '%d', stdout: '%s', stderr: '%s'" code-id-script exit-code stdout stderr)
         nil)))))