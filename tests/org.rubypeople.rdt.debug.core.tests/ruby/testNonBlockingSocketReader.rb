require 'socket'

puts "Ruby-version: #{RUBY_VERSION}"

def startCalculatorThread()
  return Thread.new {
   startTime = Time.now()
   begin
    a = 1
    loop {
      a = a + 1  
    }
   ensure
    endTime = Time.now()	
    Thread.current["operationsPerSecond"] =  (a / (endTime - startTime)).round()
   end
  }
end

calculatorThread = startCalculatorThread()
sleep 1
calculatorThread.kill
operationsPerSecond = calculatorThread["operationsPerSecond"]
puts "OperationsPerSecond: #{operationsPerSecond}"

def debug(s)
  #puts s
  #STDOUT.flush()
end

def startSocketReadLoop(operationsPerSecond)
  puts "Acception on port 12134"
  server = TCPServer.new('localhost', 12134)
  socket = server.accept
  debug("OperationsPerSecond (without socket read loop): #{operationsPerSecond}")
  socket.printf("%s\n", operationsPerSecond)
  return Thread.new {
    sleepTime = 0.1
    blockingTime = 0.001
    calculatorThread = nil
    loop {
      sleep sleepTime # non blocking
      newData, x, y = IO.select( [socket], nil, nil, blockingTime )   # blocking
      next unless newData    
      next unless newData.length > 0
      input = newData[0].gets.chomp!
      debug("Read #{input}")

      case input
	    when /^sleepTime (.*)/
  	       sleepTime = $1.to_f
	       debug("Set sleepTime to #{sleepTime}")  	       
	       
	    when /^blockingTime (.*)/
  	       blockingTime = $1.to_f
	       debug("Set blockingTime to #{blockingTime}" )
	        	         	       
	    when /^startCalculation/
           debug("Starting Calculation")
           calculatorThread = startCalculatorThread()
           
	    when /^stopCalculation/
           debug("Stopping Calculation")
           calculatorThread.kill
           debug("Stopping Calculation")
           operationsPerSecond = calculatorThread["operationsPerSecond"]
           debug("operationsPerSecond #{operationsPerSecond}")
		   socket.printf("%s\n", operationsPerSecond)
      end
  	       
    }
  }
end

startSocketReadLoop(operationsPerSecond).join()

