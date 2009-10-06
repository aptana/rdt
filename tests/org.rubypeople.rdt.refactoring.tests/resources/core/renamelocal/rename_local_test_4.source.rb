class Test
  def put firstName, lastName, email
    member = Member.new(firstName, lastName, email)
    @members << member
    @pool << member
    puts "Member: #{member}"
    print("member", member)
    member.to_a
    {:key => member}
  end
end
