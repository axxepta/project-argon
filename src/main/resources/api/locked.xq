(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

let $user := user:current()
let $lock-file := db:open($LOCK-DB, $USER-FILE)
return exists($lock-file) and exists($lock-file/*[name() = $SOURCE][text() = $PATH]) and empty($lock-file/*[name() = $SOURCE][text() = $PATH][@user = $user])