package org.gparallelizer.samples.dataflow

import org.gparallelizer.actors.pooledActors.AbstractPooledActorGroup
import org.gparallelizer.actors.pooledActors.NonDaemonPooledActorGroup
import org.gparallelizer.dataflow.DataFlowStream
import static org.gparallelizer.dataflow.operator.DataFlowOperator.operator

def getYearEndClosing(String stock, int year) {
    def url = "http://ichart.finance.yahoo.com/table.csv?s=$stock&amp;a=11&amp;b=01&amp;c=$year&amp;d=11&amp;e=31&amp;f=$year&amp;g=m;ignore=.csv"
    try {
        def data = url.toURL().text
        return data.split("\n")[1].split(",")[4].toDouble()
    } catch (all) {
        println "Could not get $stock, assuming value 0. $all.message"
        return 0
    }
}

final AbstractPooledActorGroup group = new NonDaemonPooledActorGroup(1)
final DataFlowStream stocksStream = new DataFlowStream()
final DataFlowStream pricedStocks = new DataFlowStream()

['AAPL', 'GOOG', 'IBM', 'JAVA', 'MSFT'].each {
    stocksStream << it
}

1.upto(3) {
    operator([inputs: [stocksStream], outputs: [pricedStocks]], group) {stock ->
        def price = getYearEndClosing(stock, 2008)
//        def price = 10
        bindOutput(0, [stock: stock, price: price])
    }
}

def top = [stock: 'None', price: 0.0]
operator([inputs: [pricedStocks], outputs: []], group) {pricedStock ->
    println "Received stock ${pricedStock.stock} priced to ${pricedStock.price}"
    if (top.price < pricedStock.price) {
        top = pricedStock
        println "Top stock so far is $top.stock with price ${top.price}"
    }
}

Thread.sleep 5000
group.shutdown()
