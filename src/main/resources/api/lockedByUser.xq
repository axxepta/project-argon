(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

let $user := user:current()
return db:exists($LOCK-DB, $USER-FILE) and max(db:open($LOCK-DB, $USER-FILE)//[name() = $SOURCE and text() = $PATH and @user = $user])
