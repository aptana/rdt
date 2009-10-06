##!test1
number = 5
puts number
string = "str"
puts string.to_lower
array = []
array.each do |var|
  puts var
end
##!result1
number1 = 5
puts number1
string1 = "str"
puts string1.to_lower
array1 = []
array1.each do |var|
  puts var
end
##!test2
number = 5
puts number
number = ""
puts number
##!result2
number1 = 5
puts number1
number1 = ""
puts number1
##!test3
number, string = 5, ""
puts number
puts string
##!result3
number1, string1 = 5, ""
puts number1
puts string1
##!test4
number1, string = 5, ""
puts number1
puts string
##!result4
number2, string1 = 5, ""
puts number2
puts string1