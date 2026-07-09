package com.example.data

import com.example.data.model.Article
import com.example.data.model.EarningCategory
import com.example.data.model.QuizQuestion
import com.example.data.model.SavedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Repository {

    // Saved articles state
    private val _savedArticles = MutableStateFlow<Set<String>>(emptySet())
    val savedArticles: StateFlow<Set<String>> = _savedArticles.asStateFlow()

    fun toggleSaveArticle(articleId: String) {
        val current = _savedArticles.value
        _savedArticles.value = if (current.contains(articleId)) {
            current - articleId
        } else {
            current + articleId
        }
    }

    // High quality actionable articles
    val articles: List<Article> = listOf(
        Article(
            id = "affiliate_01",
            title = "Ethical Affiliate Marketing Blueprint",
            subtitle = "Build a sustainable, trust-first affiliate business from scratch.",
            category = EarningCategory.AFFILIATE_MARKETING,
            readTimeMinutes = 8,
            difficulty = "Beginner",
            tags = listOf("Passive Income", "SEO", "Niche Sites"),
            earningPotential = "$500 - $10,000 / month",
            iconName = "Share",
            isFeatured = true,
            content = """
                Affiliate marketing is the practice of earning a commission by promoting another company's products or services. Done ethically, it is one of the most powerful business models available today.
                
                ### The Core Philosophy: Trust is Currency
                Many affiliate marketers fail because they promote junk products simply to make a quick buck. This is a dead-end strategy. Sustainable wealth is built by providing genuine value and only recommending products you have tested and fully believe in.
                
                ### Step 1: Choose Your Core Focus (Your Niche)
                Do not attempt to build an "everything" store. Focus on a specific sub-niche where you have interest or expertise. Examples:
                • Instead of "Fitness", focus on "Kettlebell training for desk workers over 40".
                • Instead of "Finance", focus on "SaaS tool reviews for freelance web designers".
                
                ### Step 2: Set Up Your Platform
                While you can start on social media, owning your distribution channel (a WordPress, Ghost, or static blog/newsletter) is critical. This protects you from algorithm changes.
                
                ### Step 3: Produce Search-Optimized, Problem-Solving Content
                Create content that answers specific search queries. Examples:
                • **"Best [Product Category] for [Specific Audience]"** (e.g., Best microphones for remote podcasting)
                • **"Product A vs. Product B Comparison"**
                • **In-depth, hands-on reviews with original photography/screenshots**
                
                ### Step 4: Sign Up for Legitimate Networks
                Start with trusted affiliate platforms:
                1. **Amazon Associates**: Great for beginners, huge catalog, but low commissions (1% - 4%).
                2. **ShareASale & CJ Affiliate**: Host thousands of intermediate brands.
                3. **Direct SaaS Affiliate Programs**: Recommend software you already use (often pay 20% - 40% recurring monthly commissions!).
                
                ### Step 5: Master the Art of Ethical Disclosure
                Always clearly disclose that you earn a small commission at no extra cost to the user. This builds deep credibility and aligns with legal regulations.
            """.trimIndent()
        ),
        Article(
            id = "freelance_01",
            title = "The $5k/Month Freelancing Roadmap",
            subtitle = "Transition from gig-platform dependency to high-paying direct clients.",
            category = EarningCategory.FREELANCING,
            readTimeMinutes = 10,
            difficulty = "Intermediate",
            tags = listOf("Freelancing", "High-Paying", "Client Acquisition"),
            earningPotential = "$1,000 - $15,000 / month",
            iconName = "Work",
            isFeatured = true,
            content = """
                Freelancing lets you trade your skills for immediate income. The challenge isn't finding work—it is escaping low-tier, price-competitive bidding sites and securing high-value contracts.
                
                ### Phase 1: Identify Your High-Income Skill (HIS)
                The market values specific, business-impacting skills far higher than generic services.
                • **Low Value**: "Writing articles" vs. **High Value**: "Writing conversion-optimized email flows for Shopify stores."
                • **Low Value**: "Building websites" vs. **High Value**: "Designing high-converting landing pages for B2B tech companies."
                
                ### Phase 2: Create a Stand-Alone Portfolio
                Do not rely on resume sheets. Build a simple 3-page personal portfolio.
                • **The Hook**: Clear headline stating WHO you help and HOW.
                • **Case Studies**: Instead of showing final screenshots, outline:
                  1. The Client's original problem.
                  2. Your custom strategic solution.
                  3. The measurable results (e.g., 22% increase in sales or 40 hours saved).
                
                ### Phase 3: Escape the Platform Trap
                Fiverr and Upwork are okay for first-month confidence, but they take 10%-20% of your earnings and force you into price bidding wars. To scale, go direct:
                1. **LinkedIn Outbound**: Search for marketing managers, content directors, or founders at target mid-sized companies. Connect and offer a quick value-add audit.
                2. **Cold Emailing**: Research 50 prospects who need your service. Send short, personalized emails showing them exactly how they could improve their current output.
                
                ### Phase 4: Master the Value-Based Pricing
                Never quote hourly rates if you can avoid it. Clients don't buy hours; they buy outcomes. Quote flat project rates.
                • If a website redesign will bring a client an extra $10,000/month, charging $3,000 for the project is an absolute bargain for them, even if it only takes you 15 hours to complete.
            """.trimIndent()
        ),
        Article(
            id = "digital_01",
            title = "Digital Product Design & Launch",
            subtitle = "Package your knowledge into high-margin assets that sell while you sleep.",
            category = EarningCategory.DIGITAL_PRODUCTS,
            readTimeMinutes = 9,
            difficulty = "Advanced",
            tags = listOf("Digital Products", "Gumroad", "Notion Templates"),
            earningPotential = "$200 - $30,000 / month",
            iconName = "ShoppingBag",
            isFeatured = false,
            content = """
                Digital products—e-books, Notion templates, checklists, video courses, or custom spreadsheets—have 99% profit margins and near-zero inventory costs. Once built, they can sell indefinitely.
                
                ### The Minimum Viable Product (MVP) Rule
                Never spend six months building a digital product in secret. It is highly likely nobody will buy it because it hasn't been validated. Build a "Minimum Viable Product" first:
                • Instead of a 10-hour masterclass video course, create a highly detailed, 15-page actionable PDF guide or a customized Notion dashboard.
                • Release it to a small group of people for free in exchange for feedback and testimonials.
                
                ### Step 1: Brainstorm Your Value Asset
                Ask yourself: What is a problem you solved for yourself that others are struggling with?
                • **Example 1**: "My personalized budget tracker that helped me clear $20k in student loans." (Target: Young adults looking to clear debt)
                • **Example 2**: "My custom Figma UI kit that saves 5 hours per design project." (Target: Figma web designers)
                
                ### Step 2: Set Up Gumroad or Lemon Squeezy
                These platforms handle payment processing, sales tax, PDF stamping, and automatic product delivery out-of-the-box. Setup takes less than 30 minutes.
                
                ### Step 3: Build the Distribution Flywheel
                Great products don't sell themselves. You need a simple marketing mechanism:
                1. **The Lead Magnet**: Give away a smaller, highly useful free version (e.g., a simple checklist) in exchange for their email address.
                2. **The Email Sequence**: Send a series of educational emails over 5 days, building up to a special discount offer for your main product.
                3. **Short-Form Content**: Post bite-sized tips, breakdowns, or tutorials on platforms like TikTok, YouTube Shorts, or LinkedIn, linking to your free lead magnet.
            """.trimIndent()
        ),
        Article(
            id = "edu_01",
            title = "The Power of Compound Interest",
            subtitle = "Uncover the mathematical miracle behind long-term wealth accumulation.",
            category = EarningCategory.FINANCIAL_EDUCATION,
            readTimeMinutes = 7,
            difficulty = "Beginner",
            tags = listOf("Investing", "Personal Finance", "Wealth Building"),
            earningPotential = "Long-term Wealth Freedom",
            iconName = "ShowChart",
            isFeatured = false,
            content = """
                All the online income in the world won't make you wealthy if you don't understand how to keep it and make it grow. The cornerstone of financial education is mastering compound interest.
                
                ### What is Compound Interest?
                Compound interest is the interest you earn on interest. Over time, it turns a linear savings graph into a steep, exponential hockey-stick curve. Albert Einstein famously called it the "Eighth Wonder of the World."
                
                ### The Simple Formula
                Every dollar you invest is a tiny employee working for you. 
                • Year 1: You invest $1,000 at a 10% average annual return. You earn $100. Your balance is $1,100.
                • Year 2: You earn 10% interest not on $1,000, but on **$1,100**. You earn $110. Your balance is $1,210.
                • Year 10: Your $1,000 has grown to $2,593 with zero extra contributions.
                • Year 30: Your $1,000 has grown to **$17,449**!
                
                ### The Cost of Delay
                Time is the absolute most critical factor in compounding.
                • **Investor A** starts at age 20, investing $200 a month for 10 years, then stops contributing entirely.
                • **Investor B** starts at age 30, investing $200 a month for 30 years until retirement.
                • At age 60, who has more money? **Investor A** has significantly more, despite only contributing for 10 years instead of 30, because their money had an extra 10-year head start to compound!
                
                ### How to Start Participating Legitimately
                1. **Eradicate High-Interest Debt**: Paying off a credit card with 20% interest is the equivalent of a guaranteed 20% tax-free investment return!
                2. **Build an Emergency Fund**: Put 3-6 months of expenses in a High-Yield Savings Account (HYSA) so you never have to pull money out of long-term investments during a crisis.
                3. **Automate Index Fund Investing**: Set up automatic monthly purchases of broad-market index funds (like S&P 500 or Total World Stock ETFs). These offer historical average returns of 8%-10% per year with highly diversified safety.
            """.trimIndent()
        ),
        Article(
            id = "edu_02",
            title = "Niche Site Monetization Strategies",
            subtitle = "Learn how to capture target search traffic and convert readers into buyers.",
            category = EarningCategory.AFFILIATE_MARKETING,
            readTimeMinutes = 6,
            difficulty = "Intermediate",
            tags = listOf("SEO", "Monetization", "Ads"),
            earningPotential = "$300 - $5,000 / month",
            iconName = "AccountBalance",
            isFeatured = false,
            content = """
                Building an audience online is great, but knowing how to monetize that audience is a science in itself. 
                
                ### The Three Pillars of Site Monetization
                Once your blog or niche site receives over 10,000 monthly visits, three main income streams unlock:
                
                1. **Display Advertising**:
                   Instead of low-paying Google AdSense, apply to premium ad networks like **Mediavine** or **Raptive** once you hit their traffic thresholds. These pay up to $20-$40 per 1,000 page views (RPM).
                
                2. **Affiliate Recommendations**:
                   Integrate contextual affiliate links naturally within your articles. If someone searches "how to clean suede shoes" and you write a detailed tutorial recommending a specific $15 brush, the buyer conversion rate will be extremely high.
                
                3. **Digital Info-Products**:
                   If your readers ask the same questions repeatedly, bundle the answers into a premium $29 e-book or printable template, capturing 100% of the margins.
                
                ### SEO SEO and SEO
                To get visitors without spending money, your articles must rank on Google. Write extremely focused, helpful, comprehensive guides that directly satisfy the searcher's query. Better user experience (longer read time, clear headings, zero spam) translates directly to higher rankings.
            """.trimIndent()
        )
    )

    // Actionable quizzes for testing knowledge
    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            category = EarningCategory.FINANCIAL_EDUCATION,
            question = "Which asset class has historically provided the most consistent long-term returns above inflation?",
            options = listOf("Physical Gold", "Broad-Market Stock Index Funds", "Cryptocurrency", "Regular Bank Savings Account"),
            correctAnswerIndex = 1,
            explanation = "Over the past 100 years, broad-market stock index funds (like the S&P 500) have returned a historical average of 8%-10% per year, consistently outperforming gold and inflation."
        ),
        QuizQuestion(
            category = EarningCategory.AFFILIATE_MARKETING,
            question = "What is the most sustainable and high-converting strategy for an affiliate marketer?",
            options = listOf(
                "Posting affiliate links in random social media comment sections",
                "Promoting every product that pays a high commission, regardless of quality",
                "Building trust by writing comprehensive, honest reviews based on hands-on testing",
                "Buying paid traffic to direct affiliate links"
            ),
            correctAnswerIndex = 2,
            explanation = "Authenticity and user trust are the ultimate metrics in affiliate marketing. Honest reviews based on real testing convert significantly better and build long-term reader loyalty."
        ),
        QuizQuestion(
            category = EarningCategory.FREELANCING,
            question = "To escape low-paying bids and earn $5,000/month as a freelancer, what should you transition to?",
            options = listOf(
                "Bidding on cheaper jobs to win more work",
                "Specializing in a high-income skill and pitching mid-sized businesses directly via LinkedIn/Email",
                "Working 90 hours a week on Fiverr",
                "Changing your profile name to sound more corporate"
            ),
            correctAnswerIndex = 1,
            explanation = "Specializing in a high-value business-impacting skill (e.g., copywriting for SaaS) and pitching companies directly removes you from platform fee cuts and intense price competition."
        ),
        QuizQuestion(
            category = EarningCategory.DIGITAL_PRODUCTS,
            question = "What is a 'Lead Magnet' in digital product marketing?",
            options = listOf(
                "A magnet that attracts metal hard drives",
                "A highly expensive product that drives 90% of your business profits",
                "A high-quality, free value asset given to readers in exchange for their email address",
                "An online advertisement that forces people to buy instantly"
            ),
            correctAnswerIndex = 2,
            explanation = "A Lead Magnet (like a free checklist or guide) builds email subscribers, allowing you to nurture relationships and pitch your main digital product via an automated email funnel."
        ),
        QuizQuestion(
            category = EarningCategory.FINANCIAL_EDUCATION,
            question = "If Investor A starts saving at 20 and Investor B starts at 30, with both earning 9% return, why does Investor A accumulate vastly more wealth?",
            options = listOf(
                "Because Investor A is naturally luckier",
                "Because they had 10 extra years for compound interest to multiply their interest on interest exponentially",
                "Because younger investors receive better tax breaks",
                "Because of stock splits"
            ),
            correctAnswerIndex = 1,
            explanation = "Time is the absolute multiplier of compound interest. A 10-year head start gives compounding interest an extra decade to double and grow exponentially, making a colossal difference."
        )
    )

    // CALCULATORS UTILITY FUNCTIONS

    /**
     * Calculates compound interest over time.
     * Formula: A = P(1 + r/n)^(nt) + PMT * (((1 + r/n)^(nt) - 1) / (r/n)) * (1 + r/n)
     */
    fun calculateCompoundInterest(
        principal: Double,
        monthlyContribution: Double,
        annualRate: Double,
        years: Int
    ): List<InterestYearData> {
        val r = annualRate / 100.0
        val n = 12 // monthly compounding
        val t = years.toDouble()
        
        val data = mutableListOf<InterestYearData>()
        var currentBalance = principal
        var totalContributions = principal

        data.add(InterestYearData(0, principal, principal, 0.0))

        for (year in 1..years) {
            for (month in 1..12) {
                // Compound the existing balance by monthly rate
                currentBalance = currentBalance * (1.0 + r / n) + monthlyContribution
                totalContributions += monthlyContribution
            }
            val totalInterest = currentBalance - totalContributions
            data.add(
                InterestYearData(
                    year = year,
                    balance = currentBalance,
                    contributions = totalContributions,
                    interestEarned = totalInterest
                )
            )
        }
        return data
    }

    /**
     * Calculates required freelance hourly rate based on target net income, expenses, taxes, and billable hours.
     */
    fun calculateFreelanceRate(
        targetMonthlyNet: Double,
        monthlyExpenses: Double,
        taxRatePercent: Double,
        billableHoursPerWeek: Double,
        vacationWeeksPerYear: Int
    ): FreelanceRateResult {
        val annualNetNeeded = targetMonthlyNet * 12
        val annualExpenses = monthlyExpenses * 12
        
        // Annual gross needed before taxes: Net = Gross * (1 - tax) -> Gross = Net / (1 - tax)
        val taxMultiplier = 1.0 - (taxRatePercent / 100.0)
        val annualGrossNeeded = if (taxMultiplier > 0) annualNetNeeded / taxMultiplier else annualNetNeeded
        val totalAnnualRevenueNeeded = annualGrossNeeded + annualExpenses

        // Total billable hours in a year: (52 - vacation weeks) * billable hours per week
        val workingWeeks = (52 - vacationWeeksPerYear).coerceAtLeast(1)
        val totalAnnualBillableHours = workingWeeks * billableHoursPerWeek

        val hourlyRateNeeded = if (totalAnnualBillableHours > 0) {
            totalAnnualRevenueNeeded / totalAnnualBillableHours
        } else {
            0.0
        }

        return FreelanceRateResult(
            hourlyRate = hourlyRateNeeded,
            annualGrossRevenueNeeded = totalAnnualRevenueNeeded,
            annualTaxAmount = totalAnnualRevenueNeeded * (taxRatePercent / 100.0),
            hoursPerYear = totalAnnualBillableHours
        )
    }

    /**
     * Evaluates a digital product idea based on four criteria (0-10 scale):
     * - Pain Point Severity (How desperately does the customer need a fix?)
     * - Market Size (Are there enough buyers willing to spend?)
     * - Production Complexity (Can you create this in a short time?)
     * - Profit Margin / Scalability (Is delivery and sales fully automated?)
     */
    fun evaluateDigitalProductIdea(
        painSeverity: Float,
        marketSize: Float,
        productionEase: Float,
        marginPotential: Float
    ): ProductEvaluationResult {
        val totalScore = (painSeverity + marketSize + productionEase + marginPotential) / 40f * 100f
        val rating = when {
            totalScore >= 80 -> "Elite Venture (Launch immediately!)"
            totalScore >= 60 -> "Strong Concept (Validate with a lead magnet first)"
            totalScore >= 40 -> "Moderate Prospect (Refine target problem or audience)"
            else -> "High Risk (Pivot to a deeper pain point or smaller scope)"
        }
        return ProductEvaluationResult(
            score = totalScore,
            rating = rating,
            breakdown = listOf(
                "Pain Point Severity" to painSeverity * 10,
                "Market Interest" to marketSize * 10,
                "Ease of Production" to productionEase * 10,
                "Scalability Potential" to marginPotential * 10
            )
        )
    }
}

data class InterestYearData(
    val year: Int,
    val balance: Double,
    val contributions: Double,
    val interestEarned: Double
)

data class FreelanceRateResult(
    val hourlyRate: Double,
    val annualGrossRevenueNeeded: Double,
    val annualTaxAmount: Double,
    val hoursPerYear: Double
)

data class ProductEvaluationResult(
    val score: Float,
    val rating: String,
    val breakdown: List<Pair<String, Float>>
)
