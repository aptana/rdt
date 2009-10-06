class Employee
  def work! kind, *rest
    puts "doing #{kind.to_s} and #{rest}"
    puts "finished"
  end
end

@e = Employee.new
@e.work! "coding", 1, 2, "huhu"