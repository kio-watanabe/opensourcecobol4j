       identification division.
       program-id. prog1.
       data division.
       working-storage section.
       procedure division.
       label-1.
           display "label-1".
           go to label-5.

       label-2.
           display "label-2".

       label-3.
           display "label-3".
      
       label-4.
           display "label-4".
       
       label-5.
           display "label-5".
      *    display "before perform".
           perform label-2 thru label-4.
      *    display "after perform".
           go to end-prog.
       
       dummy-label.
           display "this is not displayed".

       end-prog section.
           display "end-prog section".
       end-prog-1.
           display "end-1".
       end-prog-2.
           display "end-2".

       stop-run section.
           display "stop-run".
           stop run.