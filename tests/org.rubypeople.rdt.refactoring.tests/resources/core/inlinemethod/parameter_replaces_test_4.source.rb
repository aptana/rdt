class Employee
  def work! kind, t = 5
    puts "doing #{kind.to_s} #{t} times"
  end
end

@e = Employee.new
@e.work! "coding"