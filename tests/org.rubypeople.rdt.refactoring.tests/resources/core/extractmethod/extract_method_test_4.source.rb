class ReportListView < Qt::ListView

  def remove_report selected_item
    return unless selected_item
    @selected_report = selected_item
    take_item(selected_item)
    emit active_report_removed
  end
end