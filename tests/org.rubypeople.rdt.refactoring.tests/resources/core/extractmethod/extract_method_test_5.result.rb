class ReporterMainWindow < MainWindow
  def save_reports
    puts "Saving Reports..."
    @reports.each do |report|
      conv_times report
    end
  end

  def conv_times report
    report.start_t = report.start_t.to_string
    report.end_t = report.end_t.to_string
  end
  private :conv_times
  
end