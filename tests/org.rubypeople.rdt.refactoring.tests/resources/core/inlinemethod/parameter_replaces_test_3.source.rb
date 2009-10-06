class Employee
  def work! kind, t
    puts "doing #{kind.to_s} #{t} times"
  end
end

@e = Employee.new
times = 5
@e.work! "coding", times