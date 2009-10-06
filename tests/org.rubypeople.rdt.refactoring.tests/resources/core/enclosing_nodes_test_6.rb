class InsertUrlDialog
 private
  def insertClipBoardButton_pressed
    urlEdit.text = Qt::Application.clipboard.text(Qt::Clipboard::Clipboard)
  end
end