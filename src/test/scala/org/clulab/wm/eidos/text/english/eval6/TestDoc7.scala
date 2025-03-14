package org.clulab.wm.eidos.text.english.eval6

import org.clulab.wm.eidos.test.TestUtils._
import org.clulab.wm.eidos.graph._

class TestDoc7 extends EnglishTest {
  
  // Filename: FFP Fact Sheet_South Sudan_2018.01.17 BG.pdf
  // Unit test designer: Adarsh

  { // Paragraph 1
    val text = """
      After nearly four years of civil conflict, South Sudan remains one of the
      most food-insecure countries in the world. By the end of the 2017 lean
      season in September--the period of the year when food is most
      scarce--approximately 56 percent of the country's population was facing
      life-threatening hunger and in need of humanitarian assistance, making
      2017 the most food-insecure year in South Sudan's history.
      """

    val tester = new GraphTester(text)

    val conflict = NodeSpec("nearly four years of civil conflict")
    val food = NodeSpec("food", Quant("scarce", "most"))
    val hunger = NodeSpec("hunger", Quant("life-threatening"))
    val leanSeason = NodeSpec("lean season")

    behavior of "TestDoc7 Paragraph 1"

    // we're not expanding entities outside of relations
//    failingTest should "have correct singleton node 1" taggedAs(Egoitz) in {
//      tester.test(conflict) should be (successful)
//    }
    passingTest should "have correct singleton node 2" taggedAs(Egoitz) in {
      tester.test(hunger) should be (successful) 
    }
    // Note: There is not a clear path to connect "season" and "food".
    futureWorkTest should "have correct edge 1" taggedAs(Egoitz) in {
      tester.test(EdgeSpec(food, Correlation, leanSeason)) should be (successful) 
    }
  }
  { // Paragraph 2
    val text = """
      Despite slight improvements in food availability due to seasonal harvests
      from October to December, the 2018 lean seasons began in January--three
      months earlier than usual--according to the Integrated Food Security Phase
      Classification (IPC). Food security is expected to deteriorate through
      March, with an estimated 5.1 million people--nearly half of the
      population--facing Crisis (IPC 3) or worse levels of acute food insecurity.*
      """
  
    val tester = new GraphTester(text)

    val foodAvailability = NodeSpec("food availability",
                                    Inc("improvements", "slight"))
    val seasonalHarvests = NodeSpec("seasonal harvests from October", TimEx("October"))
    val leanSeasons = NodeSpec("lean seasons")
    val foodSecurity = NodeSpec("Food security", Dec("deteriorate"), TimEx("March"))
    val foodInsecurity = NodeSpec("levels of acute food insecurity", Dec("worse"))
    
    behavior of "TestDoc7 Paragraph 2"

    passingTest should "have correct singleton node 1" taggedAs(Somebody) in {
      tester.test(foodSecurity) should be (successful) 
    }
    // No longer expanding except in the context of a relevant event
//    passingTest should "have correct singleton node 2" taggedAs(Egoitz) in {
//      tester.test(foodInsecurity) should be (successful)
//    }
    passingTest should "have correct edge 1" taggedAs(Egoitz) in {
      tester.test(EdgeSpec(seasonalHarvests, Causal, foodAvailability)) should be (successful) 
    }
  }
  { // Paragraph 3
    val text = """
      In particular, ongoing conflict has severely affected areas of Western
      Bahr El Ghazal State, resulting in approximately 20,000 people
      experiencing Humanitarian Catastrophe levels of acute food insecurity--or
      famine at the household level--meaning that starvation, destitution and
      death are evident
      """

    val tester = new GraphTester(text)

    val conflict = NodeSpec("conflict", Quant("ongoing"))
    val foodInsecurity = NodeSpec("levels of acute food insecurity")
    val starvation = NodeSpec("starvation", Quant("evident"))
    val destitution = NodeSpec("destitution", Quant("evident"))
    val death = NodeSpec("death", Quant("evident"))

    behavior of "TestDoc7 Paragraph 3"

    passingTest should "have correct singleton node 1" taggedAs(Ajay) in {
      tester.test(conflict) should be (successful) 
    }
    brokenSyntaxTest should "have correct edge 1" taggedAs(Ajay) in {
      tester.test(EdgeSpec(foodInsecurity, Correlation, starvation)) should be (successful) 
    }
    brokenSyntaxTest should "have correct edge 2" taggedAs(Ajay) in {
      tester.test(EdgeSpec(foodInsecurity, Correlation, destitution)) should be (successful) 
    }
    brokenSyntaxTest should "have correct edge 3" taggedAs(Ajay) in {
      tester.test(EdgeSpec(foodInsecurity, Correlation, death)) should be (successful) 
    }
  }
  { // Paragraph 4
    val text = """
      As of January 2017, approximately 2.4 million refugees have fled South
      Sudan for neighboring countries and another 1.9 million South Sudanese
      remain internally displaced. Widespread insecurity continues to displace
      communities, disrupt livelihood activities, exacerbate food insecurity and
      impede humanitarian access to vulnerable populations.
      """
  
    val tester = new GraphTester(text)

    val refugees = NodeSpec("2.4 million refugees", Quant("approximately"), TimEx("January 2017"))
    val insecurity = NodeSpec("Widespread insecurity", Inc("Widespread"))
    val communities = NodeSpec("communities")
    val livelihoodActivities = NodeSpec("livelihood activities", Dec("disrupt"))
    val foodInsecurity = NodeSpec("food insecurity", Inc("exacerbate"))
    val humanitarianAccess = NodeSpec("humanitarian access to vulnerable populations", Dec("impede"), Quant("vulnerable"))
    
    behavior of "TestDoc7 Paragraph 4"

    passingTest should "have correct singleton node 1" taggedAs(Egoitz) in {
      tester.test(refugees) should be (successful) 
    }
    // This needs the removal of chunks in the EntityFinder...
    futureWorkTest should "have correct edge 1" taggedAs(Egoitz) in {
      tester.test(EdgeSpec(insecurity, Causal, communities)) should be (successful)
    }
    passingTest should "have correct edge 2" taggedAs(Egoitz) in {
      tester.test(EdgeSpec(insecurity, Causal, livelihoodActivities)) should be (successful) 
    }
    passingTest should "have correct edge 3" taggedAs(Egoitz) in {
      tester.test(EdgeSpec(insecurity, Causal, foodInsecurity)) should be (successful) 
    }
    passingTest should "have correct edge 4" taggedAs(Egoitz) in {
      tester.test(EdgeSpec(insecurity, Causal, humanitarianAccess)) should be (successful) 
    }
  }
  { // Paragraph 5
    val text = """
      A sustained and unimpeded humanitarian response is critical to saving
      lives and preventing a deterioration to Famine (IPC 5) levels of acute
      food insecurity. Since the start of the conflict, the USAID's Office of
      Food for Peace (FFP) and its partners--including the UN World Food Program
      (WFP) and the UN Children's Fund (UNICEF)--have responded to the needs of
      South Sudan's most vulnerable and conflict-affected populations through
      emergency food and nutrition interventions. In FY 2017, FFP-supported
      programs provided life-saving food assistance to 1.1 million people per
      month, on average.
      """
  
    val tester = new GraphTester(text)

    val humanitarianResponse = NodeSpec("humanitarian response",
                                        Quant("sustained", "unimpeded"))
    val foodInsecurity = NodeSpec("levels of acute food insecurity",
                                  Quant("deterioration"))
    
    behavior of "TestDoc7 Paragraph 5"
    // We're not expanding singleton nodes currently
//    failingTest should "have correct singleton node 1" taggedAs(Ajay) in {
//      tester.test(humanitarianResponse) should be (successful)
//    }
//    failingTest should "have correct singleton node 2" taggedAs(Ajay) in {
//      tester.test(foodInsecurity) should be (successful)
//    }
  }
  { // Paragraph 6
    val text = """
      FFP also partners with Catholic Relief Services to provide families in Jonglei
      State with emergency food assistance, expand access to safe drinking water, and
      implement livelihoods interventions, including providing agricultural training
      for farming households.
      """
  
    val tester = new GraphTester(text)

    val foodAssistance = NodeSpec("emergency food assistance")
    val water = NodeSpec("access to safe drinking water", Inc("expand"))
    val livelihoodsInterventions = NodeSpec("livelihoods interventions")
    val agriculturalTraining = NodeSpec("agricultural training")

    behavior of "TestDoc7 Paragraph 6"

    // Currently, because this node doesn't participate in events and is not modified, we prune it out.
//    failingTest should "have correct singleton node 1" taggedAs(Becky) in {
//      tester.test(foodAssistance) should be (successful)
//    }
    // We're not expanding singleton nodes currently
//    failingTest should "have correct singleton node 2" taggedAs(Becky) in {
//      tester.test(water) should be (successful)
//    }

    futureWorkTest should "have correct edge 1" taggedAs(Somebody) in {
      tester.test(EdgeSpec(agriculturalTraining, IsA, livelihoodsInterventions)) should be (successful) 
    }
  }
}
