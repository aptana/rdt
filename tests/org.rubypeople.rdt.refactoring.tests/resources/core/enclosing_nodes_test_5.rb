class HttpServer
  def reap_dead_workers(reason='unknown')
    if @workers.list.length > 0
      STDERR.puts "#{Time.now}: Reaping #{@workers.list.length} threads for slow workers because of '#{reason}'"
      error_msg = "Mongrel timed out this thread: #{reason}"
      mark = Time.now
      @workers.list.each do |w|
        w[:started_on] = Time.now if not w[:started_on]

        if mark - w[:started_on] > @death_time + @timeout
          STDERR.puts "Thread #{w.inspect} is too old, killing."
          w.raise(TimeoutError.new(error_msg))
        end
      end
    end
    return @workers.list.length
  end
end