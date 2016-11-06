(:~ List users. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

distinct-values(
        if(not(db:exists($LOCK-DB, $USER-FILE))) then () else
            db:open($LOCK-DB, $USER-FILE)/*/@user/string()
)