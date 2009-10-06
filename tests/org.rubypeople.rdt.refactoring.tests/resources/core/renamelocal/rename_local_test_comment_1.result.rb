class Test
  def put firstName, lastName, email
    #comment before assignment
    m = Member.new(firstName, lastName, email) #comment after assignment$
    #comment before usage 1
    @members << m #comment after usage 1
    #comment before usage 2
    @pool << m #comment after usage 2
    #comment before {member}
    puts "Member: #{m}" #comment after {member}
  end
end
