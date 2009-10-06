class Test
  def put firstName, lastName, email
    memberOfTheParty = Member.new(firstName, lastName, email)
    @members << memberOfTheParty
    @pool << memberOfTheParty
    puts "Member: #{memberOfTheParty}"
  end
end
