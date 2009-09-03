require 'test/unit'
require 'test/unit/ui/testrunnermediator'

def dbg;require 'rubygems';require 'ruby-debug';Debugger.start;debugger;end

def rspec_options_override
  begin
    Spec::Runner.options
  rescue NoMethodError
    rspec_options
  end  
end

module Test
  module Unit
    module UI
      module Eclipse # :nodoc:
        
        # Runs a Test::Unit::TestSuite on the console.
        class TestRunner
          
          # Creates a new TestRunner and runs the suite.
          def TestRunner.run(suite, io=STDOUT)
            return new(suite, io).start
          end
          
          # Takes care of the ARGV parsing and suite
          # determination necessary for running one of the
          # TestRunners from the command line.
          def TestRunner.start_command_line_test
            if ARGV.empty?
              puts "You should supply the name of a test suite file to the runner"
              exit
            end
            require ARGV[0].gsub(/.+::/, '')
            new(eval(ARGV[0])).start
          end
          
          # Creates a new TestRunner for running the passed
          # suite. If quiet_mode is true, the output while
          # running is limited to progress dots, errors and
          # failures, and the final result. io specifies
          # where runner output should go to; defaults to
          # STDOUT.
          def initialize(suite, io=STDOUT)
            if (suite.respond_to?(:suite))
              @suite = suite.suite
            else
              @suite = suite
            end
            @io = io
            @already_outputted = false
            @faults = []
            @tests = []
          end
          
          # Begins the test run.
          def start
            begin
              setup_mediator
              attach_to_mediator
              return start_mediator
            rescue NoMethodError, NameError # NameError in 1.6
              $stdout.puts "Launched class is not compatible with Test::Unit::TestCase"
              $stdout.flush
              output_single("%TESTC  0 v2\n")
              finished(0)
            end
          end
          
          private
          def send_tree(test)
            if(test.instance_of?(Test::Unit::TestSuite))
              notifyTestTreeEntry("#{getTestId(test)},#{escapeComma(test.to_s.strip)},true,#{test.size}")
              test.tests.each { |myTest| send_tree(myTest) }    
            else 
              notifyTestTreeEntry("#{getTestId(test)},#{escapeComma(test.name.strip)},false,#{test.size}")
            end
          end
          
          def getTestId(test)
            @tests << test
            return test.respond_to?(:object_id) ? test.object_id : test.id
          end
          
          def escapeComma(s)
            t = s.gsub(/\\/, "\\\\")
            t = t.gsub(',', "\\,")
            return t
          end
          
          def notifyTestTreeEntry(treeEntry) 
            output("%TSTTREE#{treeEntry}\n")
          end
          
          def setup_mediator # :nodoc:
            @mediator = create_mediator(@suite)
            suite_name = @suite.to_s
            if ( @suite.kind_of?(Module) )
              suite_name = @suite.name
            end
          end
          
          def create_mediator(suite) # :nodoc:
            return TestRunnerMediator.new(suite)
          end
          
          def attach_to_mediator # :nodoc:
            @mediator.add_listener(TestResult::FAULT, &method(:add_fault))
            @mediator.add_listener(TestRunnerMediator::STARTED, &method(:started))
            @mediator.add_listener(TestRunnerMediator::FINISHED, &method(:finished))
            @mediator.add_listener(TestCase::STARTED, &method(:test_started))
            @mediator.add_listener(TestCase::FINISHED, &method(:test_finished))
          end
          
          def start_mediator # :nodoc:
            return @mediator.run_suite
          end
          
          def convert_newlines(str)
            str.gsub("\\n", "\n").gsub("\\r", "\r")
          end
          
          def output_actual(msg)
            output_single("%ACTUALS \n")
            msg =~ /<"(.*)">\.$/
            actual = convert_newlines($1)
            output_single("#{actual}\n")
            output_single("%ACTUALE \n")
          end
          
          def output_expected(msg)
            output_single("%EXPECTS \n")
            msg =~ /^<"(.*?)">/
            expected = convert_newlines($1)
            output_single("#{expected}\n")
            output_single("%EXPECTE \n")
          end
          
          def add_fault(fault) # :nodoc:
            @faults << fault
            if (fault.instance_of?(Test::Unit::Failure))
              fault_type = "%FAILED "
              header = "Test::Unit::AssertionFailedError: #{fault.message}"
              stack_trace = get_location(fault.location)
            else
              fault_type = "%ERROR  "
              header = "Exception: #{fault.exception.message}"
              stack_trace = get_trace(fault.exception.backtrace)
            end
            
            if fault.message =~ /^<".*?">.*<".*?">\.$/m
              output_actual(fault.message)
              output_expected(fault.message)
            end
            
            stack_trace.gsub!(/On line #([0-9]+) of (.+)$/) {"#{$2}:#{$1}"}
            
            output_single("#{fault_type}#{@last_test_id},#{@last_test_name}\n")
            output_single("%TRACES \n")
            output_single("#{header}\n")
            output_single("#{stack_trace}\n")
            output_single("%TRACEE \n")
            @already_outputted = true
          end
          
          def get_location(location)
            location= location.join("\n") if location.is_a?(Array)
            openingBracket = location.index('[')
            if openingBracket
              return location[openingBracket + 1, location.index(']') - openingBracket].chop
            else
              # the stack trace from ruby 1.8.2 pre 3 on windows is formatted like follows:
              # file:lineNo:in 'methodName'
              return location
            end
          end
          
          def get_trace(backtrace)
            str = ""
            backtrace.each { |line| str << "#{line}\n" }
            return str
          end
          
          def started(result)
            @result = result
            output_single("%TESTC  #{@suite.size} v2\n")
            send_tree(@suite)
          end
          
          def finished(elapsed_time)
            modified_time = elapsed_time * 1000
            output("%RUNTIME#{modified_time.to_i}\n")
          end
          
          def get_test(name)
            @tests.each { |test| 
              if test.name == name
                return test
              end
            }
          end
          
          def test_started(name)
            test = get_test(name)
            @last_test_id = test.respond_to?(:object_id) ? test.object_id : test.id       
            @last_test_name = name
            output_single("%TESTS  #{@last_test_id},#{name}\n")
          end
          
          def test_finished(name)
            output_single("%TESTE  #{@last_test_id},#{name}\n")
            @already_outputted = false
          end
          
          def nl
            output("")
          end
          
          def output(something)
            @io.puts(something)
            @io.flush
          end
          
          def output_single(something)
            @io.write(something)
            @io.flush
          end
        end
      end
    end
    
    module RDT
      def self.has_tests klass
        method_names = klass.public_instance_methods(true)
        method_names.any? {|method_name| method_name =~ /^test./}
      end
      
      def self.buildSuite name
        suite = TestSuite.new(name)
        sub_suites = []
        
        ::ObjectSpace.each_object(Class) do |klass|
          if (Test::Unit::TestCase > klass && has_tests(klass))
            sub_suites << klass.suite
          end
        end
        
        sub_suites.sort! {|a,b| a.name <=> b.name }
        sub_suites.each  {|s| suite << s}
        
        suite
      end
    end
  end
end

def createTcpSession(host, port)
  
  tryCount = 0
  begin
    tryCount += 1 
    return TCPSocket.new(host, port)
  rescue Exception
    raise if tryCount == 6
    sleep(0.5)
    retry
  end
end

#if __FILE__ == $0
require 'socket'
if ARGV.empty?
  puts "You should supply the name of a test suite file and the port to the runner"
  exit
end

unless defined? NoMethodError
  # not defined in Ruby 1.6  
  class NoMethodError < NameError
  end
end


# Expect args in this order:
# 1. filename
# 2. port
# 3. keepAlive
# 4. test class name (optional)
# 5. test name (optional)
#
filename = ARGV[0].slice(0, ARGV[0].rindex('.'))
port = ARGV[1].to_i
keepAliveString = ARGV[2]
testClass = ARGV[3]
testMethod = ARGV[4]
#ARGV.clear

using_rspec = false
begin
  require filename.gsub(/.+::/, '')
  if filename =~ /spec$/ # If filename ends with spec, assume rspec
    using_rspec = true
  end
rescue NoMethodError
  require 'rubygems'
  gem 'rspec'
  require 'spec'
  require filename.gsub(/.+::/, '')
  using_rspec = true
end

session = createTcpSession('127.0.0.1', port)
if using_rspec
  require File.join(File.dirname(__FILE__), "RemoteTestRunnerRSpec.rb")
  options = rspec_options_override
  options.formatters.clear
  options.formatters << Spec::Runner::Formatter::EclipseProgressBarFormatter.new(rspec_options_override, session || STDOUT)
  options.files.delete_if{|f| true }
  Spec::Runner.use(options)
  Spec::Runner.run
else
  if (testMethod)
    testSuite = eval(testClass).new(testMethod)
  elsif testClass  
    testSuite = eval(testClass)
  else
    testSuite = Test::Unit::RDT.buildSuite filename
  end
  
  remoteTestRunner = Test::Unit::UI::Eclipse::TestRunner.new(testSuite, session || STDOUT)
  remoteTestRunner.start
end

session && session.close
