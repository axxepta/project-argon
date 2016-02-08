(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';

let $user := user:current()
let $lock-file := db:open($LOCK-DB, $LOCK-DB)
let $my-lock := $lock-file/*[name() = $SOURCE][text() = $PATH][@user = $user]

let $locks := (
    if(exists($lock-file))
    then $lock-file
    else document { <locks/> }
) update (
insert node element { $SOURCE } { attribute user { $user },  $PATH } into . )

return if(exists($lock-file) and exists($my-lock) ) then (
    ()
) else if(exists($lock-file) ) then (
    db:replace($LOCK-DB, $LOCK-DB, $locks)
)
else (
        db:add($LOCK-DB, $locks, $LOCK-DB)
)