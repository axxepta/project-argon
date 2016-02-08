(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';

not(db:exists($LOCK-DB, $LOCK-DB)) or empty(db:open($LOCK-DB)/*[name() = $SOURCE][text() = $PATH])
