(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';

let $exists := db:exists($LOCK-DB)
let $locks := (
    if($exists)
    then db:open($LOCK-DB)
    else document { <locks/> }
) update (
delete node *[name() = $SOURCE][text() = $PATH]
)
return if($exists) then (
    db:replace($LOCK-DB, $LOCK-DB, $locks)
) else (
    db:create($LOCK-DB, $locks, $LOCK-DB)
)