class Test
  def put firstName, lastName, email
    member = Member.new(firstName, lastName, email)
    @members << member
    @pool << member
    puts "Member: #{member}"
  end
end
