##!test1
@params = params
@socket = socket
content_length = self.params[Const::CONTENT_LENGTH].to_i
@remain = content_length - params.http_body.length
##!result1
@params = params
@socket = socket
content_length = replacement.params[Const::CONTENT_LENGTH].to_i
@remain = content_length - params.http_body.length
##!test2
@params = self
@socket = socket
content_length = self.params[Const::CONTENT_LENGTH].to_i
@remain = content_length - params.http_body.length
##!result2
@params = replacement
@socket = socket
content_length = replacement.params[Const::CONTENT_LENGTH].to_i
@remain = content_length - params.http_body.length
##!test3
p self
##!result3
p replacement
##!test4
return "me"
##!result4
"me"
##!test5
fac = (1..10).inject(1) { |i, j| i * j }
return fac
##!result5
fac = (1..10).inject(1) { |i, j| i * j }
fac
##!test6
@x = 5
puts @x
##!result6
a.x = 5
puts a.x