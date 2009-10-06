class Test
  def put firstName, lastName, email
    m = Member.new(firstName, lastName, email)
    @members << m
    @pool << m
    puts "Member: #{m}"
    print("member", m)
    m.to_a
    {:key => m}
  end
end
