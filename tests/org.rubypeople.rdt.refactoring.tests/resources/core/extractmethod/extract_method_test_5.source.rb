class ReporterMainWindow < MainWindow
  def save_reports
    puts "Saving Reports..."
    @reports.each do |report|
      report.start_t = report.start_t.to_string
      report.end_t = report.end_t.to_string
    end
  end
end