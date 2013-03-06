package eu.monnetproject.kap.laif.editor

import eu.monnetproject.kap.laif.LAIFController
import eu.monnetproject.framework.services.Services

class LAIFServlet extends LAIFEditor(Services.get(classOf[LAIFController])) {

}