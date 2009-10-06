class Test
  def put f, lastName, email
    member = Member.new(f, lastName, email)
    @members << member
    @pool << member
    puts "Member: #{member}"
  end
end
