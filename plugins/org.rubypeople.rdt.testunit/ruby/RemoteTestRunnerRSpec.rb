require 'rubygems'
gem 'rspec'
require 'spec'
require 'spec/runner/formatter/base_formatter'

module Spec
  module Runner   
    module Formatter
      class EclipseProgressBarFormatter < BaseFormatter
        def initialize(options, output)
          @output = output
          @uniq_ids = {}
          @uniq_id = 0
          super
        end
        
        # overridden public methods in superclass
        def start(example_count)
          send_message("TESTC", "#{example_count} v2")
        end
        
        def dump_summary(duration, example_count, failure_count, pending_count)
           send_message("RUNTIME", (duration * 1000).to_i.to_s )
        end
        
        def example_pending(example_group_name, example_name)
          if @last_example
            # we're going to trick the Test::Unit runner into displaying a 4th "pending" status
            # if we pass it a different ID for the tree entry than used by starting/stopping the test, it will show up as no status
            send_tree_entry(uniq_id(@example_group, @last_example, 'pending'), description_for_example(@last_example),false,1)
            send_example_message("TESTS", @last_example)
            puts("#{example_group_name} #{example_name}")
            send_example_stopped(@last_example)
            @last_example = nil
          end
        end
  
        def example_started(example)
          @last_example = example
          # communicate nothing - because the description might change on the spec, so we can't send any information about the spec until after it's done running
        end
        
        def example_failed(example, counter, failure)
          send_example_started(example)
          was_failure = failure.exception.is_a?(Spec::Expectations::ExpectationNotMetError)
          failure_message = was_failure ? "FAILED" : "ERROR"
          send_example_message(failure_message, example)
          send_example_trace(<<-EOF)
#{failure.exception.class}:
#{failure.exception.message}
#{clean_backtrace(failure.exception.backtrace) * "\n"}\n
          EOF
          send_example_stopped(example)
        end
        
        def example_passed(example)
          send_example_started(example)
          send_example_stopped(example)
        end
        
        def example_group_started(example_group_proxy)
          @example_group = example_group_proxy
          send_tree(example_group_proxy)
        end
        
        # custom private methods just for our special formatter
        private
        
        def send_example_started(example)
          send_tree_entry(uniq_id(@example_group, example), description_for_example(example),false,1)
          send_example_message("TESTS", example)
        end
        
        def send_example_stopped(example)
          send_example_message("TESTE", example)
        end
        
        def clean_backtrace(backtrace)
          found = false
          
          backtrace.map{|b| 
            if /RemoteTestRunner/.match(b) 
              found = true
            end
            
            found ? nil : b
          }.compact
        end
        
        def send_example_trace(text)
          send_message("TRACES")
          output_single("#{text.strip}\n")
          send_message("TRACEE")
        end
        
        def send_example_message(message_code, example)
          send_message(message_code, pad_number(uniq_id(@example_group, example)), description_for_example(example))
        end
        
        def send_message(message_code, *message_args)
          out = "%" + pad_string(message_code, 7)
          out << message_args.map{|m| escape_commas(m)} * ","
          out << "\n"
          output_single(out)
        end
        
        def send_tree(example_group)
          return false unless example_group.examples.length > 0
          path=example_group.instance_variable_get("@spec_path")
          formatted_path = path.to_s.strip.gsub(/\:[0-9]*$/, "")
          
          formatted_path = formatted_path.gsub(/^.+(\/spec\/.+)$/) {|m| $1 }
          
          send_tree_entry(uniq_id(example_group), escape_description(example_group_description), true, example_group.examples.length)
        end
        
        def send_tree_entry(tree_id, name, parent_node = false, count_children = 1)
          send_message("TSTTREE", pad_number(tree_id), name, parent_node, count_children)
        end
        
        def example_group_description
          @example_group.description
        end
        
        def description_for_example(example)
          escape_description(example.description) + "(" + escape_description(example_group_description) + ")"
        end
        
        def escape_description(s)
          s.to_s.strip.gsub(/[\n\r\)\(,"']/, "_")
        end
       
        def escape_commas(s)
          s.to_s.gsub(/\\/, "\\\\").gsub(',', "\\,")
        end
        
        def pad_number(number, count=8)
          number = number.to_s
          ("0" * (count-number.length)) + number
        end
        
        def pad_string(str, count, pad_char = " ")
          str = str.to_s
          str + (pad_char * (count - str.length))
        end
        
        # when using shared behaviors, examples will not have unique object_id's.  thus it becomes necessary to generate a new unique value based on the Suite and example_id
        def uniq_id(*args)
          @uniq_ids[args] || @uniq_ids[args] = (@uniq_id += 1)
        end
        
        def output(something)
          @output.puts(something)
          @output.flush
        end
        
        def output_single(something)
          @output.write(something)
          @output.flush
        end
      end
    end
  end
end