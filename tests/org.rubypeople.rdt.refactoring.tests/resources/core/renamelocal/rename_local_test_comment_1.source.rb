class Test
  def put firstName, lastName, email
    #comment before assignment
    member = Member.new(firstName, lastName, email) #comment after assignment$
    #comment before usage 1
    @members << member #comment after usage 1
    #comment before usage 2
    @pool << member #comment after usage 2
    #comment before {member}
    puts "Member: #{member}" #comment after {member}
  end
end
