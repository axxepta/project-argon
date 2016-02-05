(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';

let $exists := db:exists($LOCK-DB, $LOCK-DB)
let $locks := (
    if($exists)
    then db:open($LOCK-DB)
    else document { <locks/> }
) update (
insert node element {
$SOURCE
} {
attribute user { user:current() },
$PATH
} into .
)
return if($exists) then (
    db:replace($LOCK-DB, $LOCK-DB, $locks)
) else (
       db:add($LOCK-DB, $locks, $LOCK-DB)
    (: db:create($LOCK-DB, $locks, $LOCK-DB) :)
)