(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';

let $user := user:current()
return db:exists($LOCK-DB) and db:open($LOCK-DB)/*
[name() = $SOURCE][text() = $PATH][@user = $user]
