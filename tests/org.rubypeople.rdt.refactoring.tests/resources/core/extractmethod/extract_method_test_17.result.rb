def dd
  year % 400 == 0
end

leap = case
       when dd: puts x; true
       when year % 100, true == 0: false
       when year % 100 == 0: false
       else year % 4   == 0
       end
