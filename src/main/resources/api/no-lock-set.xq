(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

not(db:exists($LOCK-DB, $USER-FILE)) or empty(db:open($LOCK-DB, $USER-FILE)/*[name() = $SOURCE][text() = $PATH])
